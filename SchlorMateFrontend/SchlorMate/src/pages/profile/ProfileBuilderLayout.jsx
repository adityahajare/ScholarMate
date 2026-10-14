import { Outlet } from 'react-router-dom';
import ProfileStepper from '../../components/ProfileStepper';

export default function ProfileBuilderLayout() {
  return (
    <div className="builder-layout">
      <aside className="stepper-sidebar">
        <ProfileStepper />
      </aside>
      <main className="step-content">
        <Outlet />
      </main>
    </div>
  );
}
