export interface IApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data?: T;
}

export interface IApiErrorDetail {
  field?: string;
  message: string;
  code?: string;
}

export interface IApiErrorResponse {
  success: boolean;
  message: string;
  error?: {
    code?: string;
    details?: IApiErrorDetail[] | string | Record<string, unknown>;
    stack?: string;
  };
}

export interface IPaginationMeta {
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

export interface IPaginatedData<T> {
  items: T[];
  pagination: IPaginationMeta;
}
