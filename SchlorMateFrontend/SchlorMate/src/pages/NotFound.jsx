import { useNavigate } from "react-router-dom";
import { Compass, ArrowLeft } from "lucide-react";
import Button from "../components/common/Button";
import Card from "../components/common/Card";

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4 py-12">
      <Card className="w-full max-w-lg p-8 sm:p-10 text-center" hover={false}>
        <div className="w-14 h-14 mx-auto rounded-2xl bg-blue-50 border border-blue-100 flex items-center justify-center text-blue-600">
          <Compass className="w-7 h-7" />
        </div>

        <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight mt-5">
          Page not found
        </h1>

        <p className="text-sm text-slate-500 mt-2 leading-relaxed">
          The page you are looking for does not exist or may have been moved. Use one of the
          links below to get back to your dashboard.
        </p>

        <div className="flex flex-col sm:flex-row justify-center gap-3 mt-7">
          <Button onClick={() => navigate("/dashboard")} icon={ArrowLeft}>
            Back to dashboard
          </Button>
          <Button variant="outline" onClick={() => navigate("/scholarships")}>
            Browse scholarships
          </Button>
        </div>
      </Card>
    </div>
  );
}

export default NotFound;
