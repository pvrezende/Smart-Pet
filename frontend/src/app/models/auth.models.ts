export interface LoginPayload {
  username: string;
  password: string;
}

export interface AuthUser {
  id?: number;
  name?: string;
  username?: string;
  role?: string;
  active?: boolean;
  storeId?: number;
  storeName?: string;
}

export interface LoginResponse {
  id?: number;
  name?: string;
  username?: string;
  role?: string;
  active?: boolean;
  token: string;
  message?: string;
  storeId?: number;
  storeName?: string;
}