export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'ADMIN_STORE';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface AuthUser {
  id?: number;
  name?: string;
  username?: string;
  role?: UserRole;
  active?: boolean;
  storeId?: number | null;
  storeName?: string | null;
}

export interface LoginResponse {
  id?: number;
  name?: string;
  username?: string;
  role?: UserRole;
  active?: boolean;
  storeId?: number | null;
  storeName?: string | null;
  token: string;
  message?: string;
}

export interface MeResponse {
  id: number;
  name: string;
  username: string;
  role: UserRole;
  active: boolean;
  storeId: number | null;
  storeName: string | null;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Array<{
    field: string;
    message: string;
  }>;
}

export interface SaasBlockState {
  blocked: boolean;
  title: string;
  description: string;
  backendMessage?: string;
}