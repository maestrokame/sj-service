package sj.service.repository;

import sj.service.entity.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTypeRepository extends JpaRepository<MessageType, Integer> {
}

