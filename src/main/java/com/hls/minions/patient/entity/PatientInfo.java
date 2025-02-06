package com.hls.minions.patient.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "patients")
public record PatientInfo(
    @Id String id,
    String fullName,
    String dateOfBirth,
    String gender,
    String currentLocation,
    String emergencyContactName,
    String emergencyContactPhone,
    String knownMedicalConditions,
    String allergies,
    List<Medication> currentMedications,
    List<Medication> pastMedications,
    String mentalHealthHistory,
    String pastHospitalizations,
    String primaryCarePhysician,
    String psychiatrist,
    boolean previousEmergencyVisits
) {}

