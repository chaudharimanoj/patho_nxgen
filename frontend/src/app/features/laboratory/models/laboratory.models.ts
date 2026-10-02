export type SampleStatus =
  | 'REGISTERED'
  | 'COLLECTED'
  | 'DISPATCHED'
  | 'RECEIVED'
  | 'PROCESSING'
  | 'QUALITY_CHECK'
  | 'VERIFIED'
  | 'APPROVED'
  | 'REPORT_DELIVERED'
  | 'REJECTED';

export interface Sample {
  id: string;
  barcode: string;
  patientId: string;
  testType: string;
  status: SampleStatus;
  collectedBy?: string;
  verifiedBy?: string;
  approvedBy?: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SampleFormValues {
  barcode: string;
  patientId: string;
  testType: string;
}

export interface SampleTransitionValues {
  actorRole: string;
  note?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Transition {
  status: SampleStatus;
  label: string;
  requiredRole: string;
}

/** All allowed role-based transitions — mirrors SampleDetailPage.tsx logic */
export const ALLOWED_TRANSITIONS: Record<SampleStatus, Transition[]> = {
  REGISTERED: [
    { status: 'COLLECTED', label: 'Mark as Collected', requiredRole: 'PHLEBOTOMIST' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'PHLEBOTOMIST' },
  ],
  COLLECTED: [
    { status: 'DISPATCHED', label: 'Mark as Dispatched', requiredRole: 'PHLEBOTOMIST' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'PHLEBOTOMIST' },
  ],
  DISPATCHED: [
    { status: 'RECEIVED', label: 'Mark as Received', requiredRole: 'TECHNICIAN' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'TECHNICIAN' },
  ],
  RECEIVED: [
    { status: 'PROCESSING', label: 'Start Processing', requiredRole: 'TECHNICIAN' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'TECHNICIAN' },
  ],
  PROCESSING: [
    { status: 'QUALITY_CHECK', label: 'Move to Quality Check', requiredRole: 'TECHNICIAN' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'TECHNICIAN' },
  ],
  QUALITY_CHECK: [
    { status: 'VERIFIED', label: 'Mark as Verified', requiredRole: 'PATHOLOGIST' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'PATHOLOGIST' },
  ],
  VERIFIED: [
    { status: 'APPROVED', label: 'Approve Sample', requiredRole: 'PATHOLOGIST' },
    { status: 'REJECTED', label: 'Reject Sample', requiredRole: 'PATHOLOGIST' },
  ],
  APPROVED: [
    { status: 'REPORT_DELIVERED', label: 'Mark Report as Delivered', requiredRole: 'LAB_ADMINISTRATOR' },
  ],
  REPORT_DELIVERED: [],
  REJECTED: [],
};
