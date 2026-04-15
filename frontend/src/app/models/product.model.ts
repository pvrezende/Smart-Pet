export interface Product {
  id?: number;
  name: string;
  animalType: string;
  brand: string;
  weight: number;
  costPrice: number;
  salePrice: number;
  stock: number;
  minimumStock: number;
  active?: boolean;
}
