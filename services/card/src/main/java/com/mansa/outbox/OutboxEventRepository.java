package com.mansa.outbox;


import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' ORDER BY o.createdAt")
    List<OutboxEvent> findAndLockNextPending(Pageable pageable);

}

