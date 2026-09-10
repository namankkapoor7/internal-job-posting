export interface AppNotification {
  id?: number;
  candidateId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}
