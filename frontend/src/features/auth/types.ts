export interface AuthResponse {
  token: string
  tokenType: string
  userId: number
  membershipId: number | null
  houseId: number | null
  name: string
  positions: string[]
}

/** Authenticated user derived from the login/register response (no token). */
export interface AuthUser {
  userId: number
  membershipId: number | null
  houseId: number | null
  name: string
  positions: string[]
  isPresident: boolean
  isTreasurer: boolean
}

/** Positions that grant permissions (all others are cosmetic). */
export function isPresident(positions: string[] | undefined): boolean {
  return !!positions?.includes('President')
}
export function isTreasurer(positions: string[] | undefined): boolean {
  return !!positions?.includes('Treasurer')
}
