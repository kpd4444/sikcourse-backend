# API spec updates

## 이번에 한 작업 요약

| 분류 | 내용 |
| --- | --- |
| 음식점 동기화 개선 | `/api/dev/places/sync/restaurants` 실행 시 장소 저장 후 메뉴도 함께 동기화하도록 변경 |
| 운영용 동기화 API 추가 | `/api/admin/places/sync/restaurants`, `/api/admin/places/sync/walks` 추가 |
| 운영용 동기화 보호 | `X-Admin-Secret` 헤더와 `ADMIN_SYNC_SECRET` 설정값이 일치해야 호출 가능 |
| 추천 API 검증 | menus, places, desserts, walks 추천 API 로컬 검증 완료 |
| 영양 카드 API 확인 | `GET /api/meal-records/today/summary` 사용 |
| 식사 완료 API 확인 | `POST /api/meal-records/complete` 사용 |
| 자동 동기화 추가 | `/api/places`, 추천 메뉴/음식점/산책 코스 조회 시 DB 데이터가 비어 있으면 TourAPI 동기화를 자동 시도 |

---

# 1. 운영용 음식점 동기화

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/admin/places/sync/restaurants` |
| Description | TourAPI 음식점 데이터를 서버 DB에 동기화합니다. 음식점 저장 후 메뉴도 함께 동기화합니다. |
| Auth | Admin Secret 필요 |
| Note | 운영 서버에서 데이터를 미리 채울 때 사용합니다. 사용자 조회 API에도 자동 동기화가 있으므로 필수 선행 작업은 아닙니다. `ADMIN_SYNC_SECRET` 환경변수가 설정되어 있어야 합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| X-Admin-Secret | `your-admin-sync-secret` | O | 운영용 동기화 secret |

### 2-2. Path Variable

없음

### 2-3. Query String

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| areaCode | `39` | X | TourAPI 지역 코드. 기본값 `39` |
| sigunguCode | `4` | X | TourAPI 시군구 코드 |
| pageNo | `1` | X | TourAPI 페이지 번호. 기본값 `1` |
| numOfRows | `100` | X | 가져올 개수. 기본값 `10` |

### 2-4. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "fetchedCount": 100,
    "createdCount": 96,
    "updatedCount": 4
  }
}
```

응답 필드 설명

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| result.fetchedCount | Integer | TourAPI에서 가져온 데이터 수 |
| result.createdCount | Integer | 새로 저장된 장소 수 |
| result.updatedCount | Integer | 기존 데이터가 갱신된 장소 수 |

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 403 | COMMON_403 | 접근 권한이 없습니다. | `X-Admin-Secret` 누락, 불일치, 또는 서버 `ADMIN_SYNC_SECRET` 미설정 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | TourAPI 키 오류, 외부 API 장애, DB 오류 |

---

# 2. 운영용 산책 코스 동기화

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/admin/places/sync/walks` |
| Description | TourAPI 자연 관광지 데이터를 산책 코스 후보로 서버 DB에 동기화합니다. |
| Auth | Admin Secret 필요 |
| Note | 운영 서버에서 산책 코스 데이터를 미리 채울 때 사용합니다. 사용자 조회 API에도 자동 동기화가 있으므로 필수 선행 작업은 아닙니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| X-Admin-Secret | `your-admin-sync-secret` | O | 운영용 동기화 secret |

### 2-2. Query String

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| areaCode | `39` | X | TourAPI 지역 코드. 기본값 `39` |
| sigunguCode | `4` | X | TourAPI 시군구 코드 |
| pageNo | `1` | X | TourAPI 페이지 번호. 기본값 `1` |
| numOfRows | `100` | X | 가져올 개수. 기본값 `10` |

### 2-3. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "fetchedCount": 94,
    "createdCount": 92,
    "updatedCount": 2
  }
}
```

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 403 | COMMON_403 | 접근 권한이 없습니다. | `X-Admin-Secret` 누락, 불일치, 또는 서버 `ADMIN_SYNC_SECRET` 미설정 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | TourAPI 키 오류, 외부 API 장애, DB 오류 |

---

# 3. 사용자용 장소 목록 조회

## 사용자용 장소 목록 조회 자동 동기화

`GET /api/places?areaCode={areaCode}&sigunguCode={sigunguCode}`는 DB에 해당 지역 장소가 없으면 음식점과 산책 코스를 자동 동기화한 뒤 다시 조회합니다.

| 조건 | 동작 |
| --- | --- |
| `areaCode` 없음 | 전체 DB 장소 조회. 자동 동기화하지 않음 |
| `areaCode` 있음 + 조회 결과 있음 | DB 결과 즉시 반환 |
| `areaCode` 있음 + 조회 결과 없음 | `syncRestaurants`, `syncWalks` 자동 실행 후 재조회 |

---

# 4. 추천 메뉴 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/trips/{tripId}/recommendations/menus` |
| Description | 여행 지역 음식점의 메뉴를 사용자 건강 상태와 오늘 섭취량 기준으로 추천합니다. |
| Auth | 필요 |
| Note | 여행 지역 장소 또는 메뉴가 없으면 음식점 동기화를 자동 시도한 뒤 추천합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `3` | O | 여행 ID |

### 2-3. Query String

없음

### 2-4. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": [
    {
      "placeId": 1,
      "placeName": "제주 식당",
      "menuId": 266,
      "menuName": "고기국수",
      "menuType": "MEAL",
      "score": 92,
      "level": "EXCELLENT",
      "reasons": []
    }
  ]
}
```

응답 필드 설명

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| result[].placeId | Long | 장소 ID |
| result[].placeName | String | 장소 이름 |
| result[].menuId | Long | 메뉴 ID |
| result[].menuName | String | 메뉴 이름 |
| result[].menuType | Enum | `MEAL`, `DESSERT` |
| result[].score | Integer | 추천 점수 |
| result[].level | Enum | `EXCELLENT`, `GOOD`, `CAUTION`, `DANGER` |
| result[].reasons[].type | Enum | `CALORIE`, `SODIUM`, `SUGAR` |
| result[].reasons[].penalty | Integer | 감점 점수 |
| result[].reasons[].exceededAmount | Integer | 초과량 |

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | TRIP_404 | Trip not found. | 존재하지 않는 여행 ID 또는 본인 소유가 아닌 여행 |
| 404 | HEALTH_404 | 건강 프로필을 찾을 수 없습니다. | 건강 프로필 생성 필요 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 5. 추천 음식점 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/trips/{tripId}/recommendations/places` |
| Description | 여행 지역 음식점 중 장소별 최고 점수 메뉴를 기준으로 음식점을 추천합니다. |
| Auth | 필요 |
| Note | 내부적으로 메뉴 추천 결과를 장소별로 묶습니다. 장소 또는 메뉴가 없으면 음식점 동기화를 자동 시도합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `3` | O | 여행 ID |

### 2-3. Query String

없음

### 2-4. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": [
    {
      "placeId": 1,
      "placeName": "제주 식당",
      "addr1": "제주특별자치도 제주시 ...",
      "mapX": 126.5312,
      "mapY": 33.4996,
      "firstImage": "https://example.com/image.jpg",
      "bestMenu": {
        "placeId": 1,
        "placeName": "제주 식당",
        "menuId": 266,
        "menuName": "고기국수",
        "menuType": "MEAL",
        "score": 92,
        "level": "EXCELLENT",
        "reasons": []
      },
      "aiCourseMessage": "이 장소는 현재 섭취 균형을 고려했을 때 식사 후보로 좋아요."
    }
  ]
}
```

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | TRIP_404 | Trip not found. | 존재하지 않는 여행 ID 또는 본인 소유가 아닌 여행 |
| 404 | HEALTH_404 | 건강 프로필을 찾을 수 없습니다. | 건강 프로필 생성 필요 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 6. 추천 디저트 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/trips/{tripId}/recommendations/desserts` |
| Description | 여행 지역 내 `DESSERT` 타입 메뉴를 추천합니다. |
| Auth | 필요 |
| Note | 현재 TourAPI 음식점 메뉴 자동 동기화는 기본적으로 `MEAL`로 저장됩니다. 디저트는 별도 등록 또는 디저트 데이터가 있어야 결과가 나옵니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `3` | O | 여행 ID |

### 2-3. Query String

없음

### 2-4. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": [
    {
      "placeId": 1,
      "placeName": "카페",
      "menuId": 300,
      "menuName": "아메리카노",
      "menuType": "DESSERT",
      "score": 95,
      "level": "EXCELLENT",
      "reasons": []
    }
  ]
}
```

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | TRIP_404 | Trip not found. | 존재하지 않는 여행 ID 또는 본인 소유가 아닌 여행 |
| 404 | HEALTH_404 | 건강 프로필을 찾을 수 없습니다. | 건강 프로필 생성 필요 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 7. 추천 산책 코스 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/trips/{tripId}/recommendations/walks` |
| Description | 여행 지역에 동기화된 산책 코스 후보를 조회합니다. |
| Auth | 필요 |
| Note | 산책 코스가 없으면 산책 코스 동기화를 자동 시도한 뒤 조회합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `3` | O | 여행 ID |

### 2-3. Query String

없음

### 2-4. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": [
    {
      "placeId": 11,
      "placeName": "오름 산책로",
      "addr1": "제주특별자치도 제주시 ...",
      "mapX": 126.2466,
      "mapY": 33.3057,
      "firstImage": "https://example.com/image.jpg"
    }
  ]
}
```

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | TRIP_404 | Trip not found. | 존재하지 않는 여행 ID 또는 본인 소유가 아닌 여행 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 8. 오늘 영양 요약 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/meal-records/today/summary` |
| Description | 오늘 등록된 식사 기록을 기준으로 칼로리, 나트륨, 당류 섭취량과 목표 대비 잔여량을 조회합니다. |
| Auth | 필요 |
| Note | 프론트 영양 카드의 `섭취량 / 목표량` 표시에 사용합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

없음

### 2-3. Query String

없음

### 2-4. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "calorieGoal": 2300,
    "calorieConsumed": 1930,
    "calorieRemaining": 370,
    "sodiumGoal": 2000,
    "sodiumConsumed": 1850,
    "sodiumRemaining": 150,
    "sugarGoal": 60,
    "sugarConsumed": 52,
    "sugarRemaining": 8,
    "carePointMessage": "오늘은 나트륨 섭취가 목표에 가까워요."
  }
}
```

응답 필드 설명

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| result.calorieGoal | Integer | 일일 칼로리 목표 |
| result.calorieConsumed | Integer | 오늘 칼로리 섭취량 |
| result.calorieRemaining | Integer | 남은 칼로리 |
| result.sodiumGoal | Integer | 일일 나트륨 목표 |
| result.sodiumConsumed | Integer | 오늘 나트륨 섭취량 |
| result.sodiumRemaining | Integer | 남은 나트륨 |
| result.sugarGoal | Integer | 일일 당류 목표 |
| result.sugarConsumed | Integer | 오늘 당류 섭취량 |
| result.sugarRemaining | Integer | 남은 당류 |
| result.carePointMessage | String | 오늘 영양 상태 안내 문구 |

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | HEALTH_404 | 건강 프로필을 찾을 수 없습니다. | 건강 프로필 생성 필요 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 9. 식사 완료

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/meal-records/complete` |
| Description | 추천 메뉴를 먹은 것으로 기록하고, 갱신된 오늘 영양 요약과 후속 추천 가능 여부를 반환합니다. |
| Auth | 필요 |
| Note | 프론트의 "먹었어요", "식사 완료" 버튼에 연결합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |
| Content-Type | `application/json` | O | JSON 요청 |

### 2-2. Path Variable

없음

### 2-3. Query String

없음

### 2-4. Request Body

```json
{
  "tripId": 3,
  "menuId": 266,
  "mealType": "LUNCH",
  "eatenAt": "2026-09-20T12:00:00"
}
```

| **Field** | **Type** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | Long | O | 여행 ID |
| menuId | Long | O | 먹은 메뉴 ID |
| mealType | Enum | O | `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` |
| eatenAt | LocalDateTime | O | 식사 시간. 미래 시간은 불가 |

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "mealRecord": {
      "mealRecordId": 2,
      "menuId": 266,
      "menuName": "고기국수",
      "mealType": "LUNCH",
      "eatenAt": "2026-09-20T12:00:00",
      "calories": 450,
      "sodium": 1100,
      "sugar": 15
    },
    "nutritionSummary": {
      "calorieGoal": 2300,
      "calorieConsumed": 450,
      "calorieRemaining": 1850,
      "sodiumGoal": 2000,
      "sodiumConsumed": 1100,
      "sodiumRemaining": 900,
      "sugarGoal": 60,
      "sugarConsumed": 15,
      "sugarRemaining": 45,
      "carePointMessage": "오늘은 주요 영양 목표를 비교적 잘 관리했어요."
    },
    "dessertAvailable": true,
    "walkAvailable": true,
    "popupMessage": "식사 기록이 완료되었습니다."
  }
}
```

응답 필드 설명

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| result.mealRecord | Object | 생성된 식사 기록 |
| result.nutritionSummary | Object | 식사 완료 후 갱신된 오늘 영양 요약 |
| result.dessertAvailable | Boolean | 해당 여행 지역에서 디저트 추천 가능 여부 |
| result.walkAvailable | Boolean | 해당 여행 지역에서 산책 코스 추천 가능 여부 |
| result.popupMessage | String | 완료 후 사용자에게 보여줄 안내 문구 |

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | 필수값 누락 또는 enum 값 오류 |
| 400 | MEAL_400 | 유효하지 않은 식사 시간입니다. | `eatenAt`이 현재보다 미래인 경우 |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | TRIP_404 | Trip not found. | 존재하지 않는 여행 ID 또는 본인 소유가 아닌 여행 |
| 404 | MEAL_404 | 메뉴를 찾을 수 없습니다. | 존재하지 않는 메뉴 ID |
| 404 | HEALTH_404 | 건강 프로필을 찾을 수 없습니다. | 건강 프로필 생성 필요 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 10. 식사 기록 등록

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/meal-records` |
| Description | 특정 메뉴를 먹은 기록으로 등록합니다. |
| Auth | 필요 |
| Note | 여행 완료 플로우가 필요 없을 때 사용합니다. 성공 후 영양 카드는 `/api/meal-records/today/summary`를 다시 조회해 갱신합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |
| Content-Type | `application/json` | O | JSON 요청 |

### 2-2. Request Body

```json
{
  "menuId": 266,
  "mealType": "LUNCH",
  "eatenAt": "2026-09-20T12:00:00"
}
```

| **Field** | **Type** | **Required** | **Description** |
| --- | --- | --- | --- |
| menuId | Long | O | 먹은 메뉴 ID |
| mealType | Enum | O | `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` |
| eatenAt | LocalDateTime | O | 식사 시간. 미래 시간은 불가 |

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "mealRecordId": 2,
    "menuId": 266,
    "menuName": "고기국수",
    "mealType": "LUNCH",
    "eatenAt": "2026-09-20T12:00:00",
    "calories": 450,
    "sodium": 1100,
    "sugar": 15
  }
}
```

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | 필수값 누락 또는 enum 값 오류 |
| 400 | MEAL_400 | 유효하지 않은 식사 시간입니다. | `eatenAt`이 현재보다 미래인 경우 |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | MEAL_404 | 메뉴를 찾을 수 없습니다. | 존재하지 않는 메뉴 ID |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 11. 오늘 식사 기록 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/meal-records/today` |
| Description | 오늘 등록된 식사 기록 목록을 조회합니다. |
| Auth | 필요 |
| Note | 영양 요약의 상세 내역 화면에서 사용할 수 있습니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Request Body

없음

---

## 3. Response

### 3-1. Success Response

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": [
    {
      "mealRecordId": 2,
      "menuId": 266,
      "menuName": "고기국수",
      "mealType": "LUNCH",
      "eatenAt": "2026-09-20T12:00:00",
      "calories": 450,
      "sodium": 1100,
      "sugar": 15
    }
  ]
}
```

### 3-2. Error Response

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |
