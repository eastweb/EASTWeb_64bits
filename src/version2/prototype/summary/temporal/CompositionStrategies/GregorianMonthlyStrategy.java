package version2.prototype.summary.temporal.CompositionStrategies;

import java.time.LocalDate;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

public class GregorianMonthlyStrategy implements TemporalSummaryCompositionStrategy {

    @Override
    public LocalDate getStartDate(LocalDate sDate) throws Exception {
        //        return new GregorianCalendar(sDate.getYear(), Calendar.MONTH, Calendar.DAY_OF_MONTH);
        return LocalDate.of(sDate.getYear(), sDate.getMonthValue(), 1);
    }

    @Override
    public int getDaysInThisComposite(LocalDate dateInComposite) {
        //        return dateInComposite.getActualMaximum(Calendar.DAY_OF_MONTH);
        return dateInComposite.lengthOfMonth();
    }

    @Override
    public int
    getCompositeIndex(LocalDate startDate, LocalDate dateInComposite) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfCompleteCompositesInRange(LocalDate startDate,
            LocalDate endDate, int daysPerInputData) {
        // TODO Auto-generated method stub
        return 0;
    }

}