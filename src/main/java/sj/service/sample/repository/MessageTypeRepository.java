package sj.service.sample.repository;

import sj.service.sample.entity.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTypeRepository extends JpaRepository<MessageType, Integer> {
}

