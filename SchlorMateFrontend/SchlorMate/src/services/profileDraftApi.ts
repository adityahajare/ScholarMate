import api from '../lib/api';

export interface ProfileDraftRequest {
  data: Record<string, any>;
}

export interface ProfileDraftResponse {
  id: number;
  userId: number;
  data: Record<string, any>;
  createdAt: string;
  updatedAt: string;
}

/** GET current draft (creates on‑fly if missing) */
export const getDraft = () =>
  api.get<ProfileDraftResponse>('/profile/draft')
    .then(res => res.data);

/** POST – explicit create (rarely used) */
export const createDraft = (payload: ProfileDraftRequest) =>
  api.post<ProfileDraftResponse>('/profile/draft', payload)
    .then(res => res.data);

/** PUT – update existing draft */
export const updateDraft = (payload: ProfileDraftRequest) =>
  api.put<ProfileDraftResponse>('/profile/draft', payload)
    .then(res => res.data);
