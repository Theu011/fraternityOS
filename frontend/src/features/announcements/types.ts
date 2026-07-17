export interface Announcement {
  id: number
  title: string
  content: string
  pinned: boolean
  authorId: number
  authorName: string | null
  createdAt: string
  updatedAt: string
}

export interface AnnouncementInput {
  title: string
  content: string
  pinned: boolean
}
