export interface SubscriptionUsage {
  planCode: string;
  planName: string;
  branchScope: string;
  volumeTierCode: string;
  volumeTierName: string;
  monthlyPatientLimit: number | null;
  currentMonthPatientCount: number;
  usagePercentage: number;
  isUnlimited: boolean;
  features: Record<string, boolean>;
  status: string;
  renewsAt: string;
}

export interface SubscriptionPlanCatalogItem {
  id: string;
  code: string;
  name: string;
  branchScope: string;
  features: string | Record<string, boolean>;
  displayOrder: number;
}

export interface VolumeTierCatalogItem {
  id: string;
  code: string;
  name: string;
  monthlyPatientLimit: number | null;
  description: string;
  displayOrder: number;
}

export interface PlanCatalog {
  plans: SubscriptionPlanCatalogItem[];
  volumeTiers: VolumeTierCatalogItem[];
}

export interface UpdateSubscriptionRequest {
  planCode: string;
  volumeTierCode: string;
}
