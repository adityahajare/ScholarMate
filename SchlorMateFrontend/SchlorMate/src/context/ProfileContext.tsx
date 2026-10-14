import React, { createContext, useContext, useEffect, useState } from 'react';
import { getDraft, updateDraft, ProfileDraftRequest } from '../services/profileDraftApi';
import { useAuth } from './AuthContext';

type DraftData = Record<string, any>;

interface ProfileContextProps {
  draft: DraftData;
  setDraft: (data: DraftData) => void;
  saveStep: (stepKey: string, stepData: any) => Promise<void>;
  loading: boolean;
}

const ProfileContext = createContext<ProfileContextProps | undefined>(undefined);

export const ProfileProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [draft, setDraftState] = useState<DraftData>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      setDraftState({});
      setLoading(false);
      return undefined;
    }
    getDraft()
      .then(response => {
        setDraftState(response.data ?? {});
      })
      .catch(() => setDraftState({}))
      .finally(() => setLoading(false));
    return undefined;
  }, [user]);

  const saveStep = async (stepKey: string, stepData: any) => {
    const newDraft = { ...draft, [stepKey]: stepData, meta: { ...(draft.meta ?? {}), updatedAt: new Date().toISOString() } };
    await updateDraft({ data: newDraft } as ProfileDraftRequest);
    setDraftState(newDraft);
  };

  return (
    <ProfileContext.Provider value={{ draft, setDraft: setDraftState, saveStep, loading }}>
      {children}
    </ProfileContext.Provider>
  );
};

export const useProfile = (): ProfileContextProps => {
  const ctx = useContext(ProfileContext);
  if (!ctx) throw new Error('useProfile must be used within ProfileProvider');
  return ctx;
};
