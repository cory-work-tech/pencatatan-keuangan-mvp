package cory.dev.pencatatan_keuangan.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class FinancialRecord {
    private LocalDate date;
    private BigDecimal balance;
    private BigDecimal change;

     
    public String getFormattedBalance() {
        return formatCurrency(balance);
    }
    
    public String getFormattedChange() {
        if (change == null) {
            return "Rp 0";
        }
        
        String sign = change.signum() > 0 ? "+" : change.signum() < 0 ? "-" : "";
        BigDecimal absChange = change.abs();
        String formatted = formatCurrency(absChange);
        
        return sign + formatted;
    }
    
    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Rp 0";
        }
        
        String numberStr = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        StringBuilder formatted = new StringBuilder();
        
        int count = 0;
        for (int i = numberStr.length() - 1; i >= 0; i--) {
            if (count == 3) {
                formatted.insert(0, '.');
                count = 0;
            }
            formatted.insert(0, numberStr.charAt(i));
            count++;
        }
        
        return "Rp " + formatted.toString();
    }
}
