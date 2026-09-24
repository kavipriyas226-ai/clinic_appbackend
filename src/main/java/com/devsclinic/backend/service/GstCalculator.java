package com.devsclinic.backend.service;

import com.devsclinic.backend.model.LineItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for invoice GST math: allocate the invoice-level discount
 * proportionally to each line, tax each line at its own configured GST rate, and split
 * that into CGST+SGST (intra-state) or IGST (inter-state). {@link com.devsclinic.backend.service.BillingService}
 * is the only caller today — this stays a separate, stateless class specifically so no
 * future caller ever reimplements this math differently.
 */
public final class GstCalculator {

    private GstCalculator() {
    }

    public record Result(
            List<LineItem> lineItems,
            double subtotal,
            double discountAmount,
            double taxableTotal,
            double cgstAmount,
            double sgstAmount,
            double igstAmount,
            double totalGst,
            double total
    ) {
    }

    public static Result calculate(
            List<LineItem> rawLineItems,
            boolean discountEnabled,
            double discountPercent,
            boolean gstEnabled,
            String supplyType
    ) {
        boolean interState = "Inter-State".equalsIgnoreCase(supplyType);

        double subtotal = 0;
        double discountAmount = 0;
        double cgstTotal = 0;
        double sgstTotal = 0;
        double igstTotal = 0;
        List<LineItem> computed = new ArrayList<>();

        for (LineItem raw : rawLineItems) {
            double lineAmount = raw.getAmount();
            double lineDiscount = discountEnabled ? round2(lineAmount * discountPercent / 100) : 0;
            double taxable = round2(lineAmount - lineDiscount);
            double rate = gstEnabled ? raw.getGstRate() : 0;
            double gstAmt = round2(taxable * rate / 100);

            double cgst = 0;
            double sgst = 0;
            double igst = 0;
            if (gstEnabled && rate > 0) {
                if (interState) {
                    igst = gstAmt;
                } else {
                    cgst = round2(gstAmt / 2);
                    // Derive SGST from what's left rather than rounding independently,
                    // so cgst + sgst always equals gstAmt exactly (no stray paisa).
                    sgst = round2(gstAmt - cgst);
                }
            }

            subtotal += lineAmount;
            discountAmount += lineDiscount;
            cgstTotal += cgst;
            sgstTotal += sgst;
            igstTotal += igst;

            computed.add(LineItem.builder()
                    .refId(raw.getRefId())
                    .type(raw.getType())
                    .name(raw.getName())
                    .price(raw.getPrice())
                    .qty(raw.getQty())
                    .amount(raw.getAmount())
                    .gstRate(rate)
                    .taxableAmount(taxable)
                    .cgstAmount(cgst)
                    .sgstAmount(sgst)
                    .igstAmount(igst)
                    .gstAmount(gstAmt)
                    .build());
        }

        subtotal = round2(subtotal);
        discountAmount = round2(discountAmount);
        cgstTotal = round2(cgstTotal);
        sgstTotal = round2(sgstTotal);
        igstTotal = round2(igstTotal);
        double totalGst = round2(cgstTotal + sgstTotal + igstTotal);
        double taxableTotal = round2(subtotal - discountAmount);
        double total = round2(taxableTotal + totalGst);

        return new Result(computed, subtotal, discountAmount, taxableTotal, cgstTotal, sgstTotal, igstTotal, totalGst, total);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
