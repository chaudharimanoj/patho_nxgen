export interface Patient {
  id: string;
  mrn: string;
  fullName: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER' | 'UNDISCLOSED';
  phone?: string;
  email?: string;
  address?: string;
  insuranceProvider?: string;
  corporateAccountCode?: string;
  createdAt: string;
}

export interface PatientFormValues {
  fullName: string;
  dateOfBirth: string;
  gender: string;
  phone?: string;
  email?: string;
  address?: string;
  insuranceProvider?: string;
  corporateAccountCode?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
