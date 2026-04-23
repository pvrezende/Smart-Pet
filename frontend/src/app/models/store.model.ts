export interface Store {
  id?: number;
  name: string;
  code: string;
  address: string;
  phone: string;
  active?: boolean;
}

export interface CreateStorePayload {
  name: string;
  code: string;
  address: string;
  phone: string;
}