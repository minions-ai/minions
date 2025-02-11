package com.hls.minions.patient.entity;

public  record Medication(
    String name,
    String dosage,
    String prescribedBy
) {}
