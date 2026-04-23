export interface Store {
  id: string
  name: string
  location: string
  createdAt: string
}

export interface StoreListResponse {
  stores: Store[]
  totalCount: number
}
