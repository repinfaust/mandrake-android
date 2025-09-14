package com.repinfaust.mandrake.domain

/**
 * TRIAGE SYSTEM - Structured, Static Screening
 * 
 * Responsibility: Capture baseline indicators at fixed intervals
 * Input: Questionnaire responses (AUDIT-C, SDS, red flag checks)
 * Output: Risk banding at point in time (Low/Elevated/High)
 * Frequency: 14-day intervals, app launch, or after high-intensity events
 * Storage: Stored locally, linked to screening date
 * 
 * This is SEPARATE from Risk Evaluation Engine which handles dynamic behavioral monitoring
 * 
 * SEPARATION OF RESPONSIBILITY:
 * ┌─────────────────┬─────────────────────────────────┬─────────────────────┬──────────────────────────┐
 * │ Component       │ Input Source                    │ Frequency           │ Output Type              │
 * ├─────────────────┼─────────────────────────────────┼─────────────────────┼──────────────────────────┤
 * │ TRIAGE          │ AUDIT-C, SDS, red flags         │ 14-day intervals    │ Risk band at snapshot    │
 * │ RISK ENGINE     │ Urge logs, event patterns       │ Every log + rolling │ Escalation nudges        │
 * └─────────────────┴─────────────────────────────────┴─────────────────────┴──────────────────────────┘
 */

// All triage functionality is implemented in:
// - ScreeningQuestions.kt (questionnaires and scoring)  
// - ScreeningQuestionnaire.kt (UI components)
// - ScreeningSheet.kt (dialog implementation)
// - RiskAssessmentRepository.kt (screening operations)

// This file serves as documentation for the system separation