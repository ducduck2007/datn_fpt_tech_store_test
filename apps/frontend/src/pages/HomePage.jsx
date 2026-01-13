import axiosClient from "../api/axiosClient";

export default function HomePage() {
  const callProfile = async () => {
    const res = await axiosClient.get("/me");
    alert(JSON.stringify(res.data));
  };

  return (
    <div style={{ padding: 24 }}>
      <h2>Home (Protected)</h2>
      <button onClick={callProfile}>Call /me</button>
    </div>
  );
}
