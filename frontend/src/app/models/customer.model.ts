export interface Customer {
  id?: number;
  name: string;
  cpf: string;
  phone: string;
  email: string;
  address: string;
  active?: boolean;
}
