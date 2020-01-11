package iansteph.nhlp3.descheduler.model.event;

import java.util.List;

public interface DeschedulerEvent {

    List<? extends Record> getRecords();
    String getPlayEventString();
}
