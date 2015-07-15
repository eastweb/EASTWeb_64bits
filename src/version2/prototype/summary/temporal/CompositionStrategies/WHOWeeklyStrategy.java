package version2.prototype.summary.temporal.CompositionStrategies;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

public class WHOWeeklyStrategy implements TemporalSummaryCompositionStrategy {

    @Override
    public LocalDate getStartDate(LocalDate sDate) throws Exception {
        //        int firstDay = GregorianCalendar.MONDAY;
        //        int currentDay = sDate.get(GregorianCalendar.DAY_OF_WEEK);
        //        if(currentDay !=  firstDay){
        //            GregorianCalendar newDate = (GregorianCalendar) sDate.clone();
        //
        //            do{
        //                newDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
        //                currentDay = newDate.get(GregorianCalendar.DAY_OF_WEEK);
        //            }while(currentDay != firstDay);
        //
        //            return newDate;
        //        }
        //        return sDate;
        LocalDate outDate = sDate;
        DayOfWeek currentDay = sDate.getDayOfWeek();
        DayOfWeek firstDay = DayOfWeek.MONDAY;
        if(currentDay != firstDay)
        {
            outDate = sDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return outDate;
    }

    @Override
    public int getDaysInThisComposite(LocalDate dateInComposite) {
        return 7;
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