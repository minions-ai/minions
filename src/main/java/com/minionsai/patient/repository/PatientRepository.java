package com.minionsai.patient.repository;

import com.minionsai.patient.entity.PatientInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends MongoRepository<PatientInfo, String> {
  Optional<PatientInfo> findByFullName(String fullName);
  Optional<PatientInfo> findByKnownMedicalConditions(String condition);

  @Aggregation("{ $sample: { size: 1 } }")
  Optional<PatientInfo> findRandomPatient();
}
