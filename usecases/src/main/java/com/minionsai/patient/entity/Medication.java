package com.minionsai.patient.entity;

public  record Medication(
    String name,
    String dosage,
    String prescribedBy
) {}
