# Test File
# 1. Hiến máu nhân đạo (DONATE_BLOOD) - Cộng 50 điểm
curl -X POST "http://localhost:8080/api/v1/ingestion/activities" \
-H "Content-Type: application/json" \
-d "{ \"cccdNumber\": \"001099000123\", \"activityCode\": \"DONATE_BLOOD\", \"sourceSystem\": \"MOH_PORTAL\", \"externalReference\": \"TXN-1001\" }"

# 2. Hoàn thành thuế đúng hạn (PAY_TAX_ON_TIME) - Cộng 20 điểm
curl -X POST "http://localhost:8080/api/v1/ingestion/activities" \
-H "Content-Type: application/json" \
-d "{ \"cccdNumber\": \"001099000123\", \"activityCode\": \"PAY_TAX_ON_TIME\", \"sourceSystem\": \"TAX_PORTAL\", \"externalReference\": \"TXN-1002\" }"
