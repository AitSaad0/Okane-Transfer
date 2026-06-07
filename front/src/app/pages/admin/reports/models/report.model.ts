export interface DailyReportDto {
  corridor: string;
  transactionCount: number;
  totalVolume: number;
  totalRevenue: number;
  agencyCommission: number;
  centralCommission: number;
}

export interface MonthlyReportDto {
  month: string;
  corridor: string;
  transactionCount: number;
  totalVolume: number;
  totalRevenue: number;
}

export interface CorridorPerformanceDto {
  corridor: string;
  totalTransactions: number;
  totalVolume: number;
  averageTransfer: number;
  generatedFees: number;
}
