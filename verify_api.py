import requests
import json

BASE_URL = "https://api.openf1.org/v1"

def test_api():
    print("Fetching sessions for 2023...")
    try:
        response = requests.get(f"{BASE_URL}/sessions?year=2023&session_type=Race")
        sessions = response.json()
        print(f"Found {len(sessions)} sessions.")
    except Exception as e:
        print(f"Error fetching sessions: {e}")
        return

    for session in sessions[:3]:  # Check first 3 sessions
        session_key = session['session_key']
        print(f"\nChecking Session: {session['session_name']} ({session_key})")
        
        # Check Telemetry
        try:
            telemetry = requests.get(f"{BASE_URL}/car_data?session_key={session_key}&limit=1").json()
            if telemetry:
                print("  [OK] Telemetry Data Found")
                print("  Telemetry Keys:", list(telemetry[0].keys()))
            else:
                print("  [FAIL] No Telemetry Data")
        except Exception as e:
            print(f"  [ERROR] Telemetry Check Failed: {e}")

        # Check Laps
        try:
            laps = requests.get(f"{BASE_URL}/laps?session_key={session_key}&limit=1").json()
            if laps:
                print("  [OK] Laps Data Found")
                print("  Laps Keys:", list(laps[0].keys()))
            else:
                print("  [FAIL] No Laps Data")
        except Exception as e:
            print(f"  [ERROR] Laps Check Failed: {e}")

if __name__ == "__main__":
    test_api()
