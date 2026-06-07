package com.camplink.entity;

/// Review state of a service-provider (seller / rider / driver) application.
/// Null on a user row means the account type does not require verification
/// (buyers and admins).
public enum VerificationStatus { PENDING, APPROVED, REJECTED }
