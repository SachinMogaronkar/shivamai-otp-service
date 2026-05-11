import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
  BarChart,
  Bar,
  Area
} from "recharts";

/* ================= CUSTOM TOOLTIP ================= */

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div style={{
        background: "white",
        padding: "10px 12px",
        borderRadius: "10px",
        boxShadow: "0 8px 20px rgba(0,0,0,0.08)"
      }}>
        <p style={{ fontWeight: 600 }}>{label}</p>

        {payload.map((p, i) => (
          <p key={i} style={{ color: p.color }}>
            {p.name}: {p.value}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

/* ================= COMPONENT ================= */

export default function UsageChart({ data }) {

  if (!data || data.length === 0) {
    return (
      <div className="chart-box">
        <p>No data available</p>
      </div>
    );
  }

  const chartData = data.map(d => ({
    date: d.date,
    requests: d.otpRequests,
    verified: d.otpVerified,
    failed: d.otpRequests - d.otpVerified
  }));

  return (
    <div className="chart-wrapper">

      {/* ===== TREND ===== */}
      <div className="chart-box">
        <h3 className="section-title">OTP Requests Trend</h3>

        <ResponsiveContainer width="100%" height={260}>
          <LineChart data={chartData}>

            <defs>
              <linearGradient id="reqGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#2563eb" stopOpacity={0.4}/>
                <stop offset="95%" stopColor="#2563eb" stopOpacity={0}/>
              </linearGradient>
            </defs>

            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip content={<CustomTooltip />} />

            <Line
              type="monotone"
              dataKey="requests"
              stroke="#2563eb"
              strokeWidth={3}
            />

            <Area
              type="monotone"
              dataKey="requests"
              fill="url(#reqGradient)"
              stroke="none"
            />

          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* ===== BAR ===== */}
      <div className="chart-box">
        <h3 className="section-title">Verified vs Failed</h3>

        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={chartData}>

            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip content={<CustomTooltip />} />

            <Bar
              dataKey="verified"
              fill="#22c55e"
              radius={[6, 6, 0, 0]}
            />

            <Bar
              dataKey="failed"
              fill="#ef4444"
              radius={[6, 6, 0, 0]}
            />

          </BarChart>
        </ResponsiveContainer>
      </div>

    </div>
  );
}