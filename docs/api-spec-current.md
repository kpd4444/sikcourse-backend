# Sikcourse API Current Spec

이 문서는 현재 백엔드 기준으로 프론트에 공유할 최신 API 명세입니다.

---

# 1. 여행지명 지역 코드 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/destinations/resolve` |
| Description | 사용자가 입력한 여행지명으로 TourAPI `areaCode`, `sigunguCode`를 찾습니다. |
| Auth | 불필요 |
| Note | DB 동기화 데이터와 무관하게 TourAPI를 직접 조회합니다. 여행 생성 또는 장소 목록 조회 전에 지역 코드를 찾는 용도로 사용합니다. |

---

## 2. Request

### 2-1. Header

없음

### 2-2. Path Variable

없음

### 2-3. Query String

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| query | `제주 성산` | O | 사용자가 입력한 여행지명 또는 키워드 |

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
    "query": "제주 성산",
    "areaCode": "39",
    "areaName": "제주특별자치도",
    "sigunguCode": "4",
    "sigunguName": "제주시"
  }
}
```

매칭되는 시군구를 찾지 못한 경우 `sigunguCode`, `sigunguName`은 `null`일 수 있습니다.

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "query": "제주",
    "areaCode": "39",
    "areaName": "제주특별자치도",
    "sigunguCode": null,
    "sigunguName": null
  }
}
```

지역 자체를 찾지 못한 경우 지역 코드 필드가 모두 `null`일 수 있습니다.

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": {
    "query": "없는지역",
    "areaCode": null,
    "areaName": null,
    "sigunguCode": null,
    "sigunguName": null
  }
}
```

응답 필드 설명

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| result.query | String | 요청으로 전달한 원본 검색어 |
| result.areaCode | String | TourAPI 지역 코드 |
| result.areaName | String | TourAPI 지역명 |
| result.sigunguCode | String | TourAPI 시군구 코드 |
| result.sigunguName | String | TourAPI 시군구명 |

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "COMMON_400",
  "message": "검증 실패 메시지",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `query` 누락 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | TourAPI 장애 또는 서버 내부 오류 |

---

# 2. 장소 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/places` |
| Description | 지역 코드와 장소 타입 조건으로 장소 목록을 조회합니다. 해당 지역 데이터가 없으면 TourAPI 동기화를 자동 시도한 뒤 다시 조회합니다. |
| Auth | 불필요 |
| Note | `areaCode`가 없으면 전체 DB 장소만 조회하고 자동 동기화하지 않습니다. 자동 동기화 실패 시 기존 DB 조회 결과를 반환하며, 기존 데이터도 없으면 빈 배열을 반환합니다. |

---

## 2. Request

### 2-1. Header

없음

### 2-2. Path Variable

없음

### 2-3. Query String

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| areaCode | `39` | X | TourAPI 지역 코드 |
| sigunguCode | `4` | X | TourAPI 시군구 코드 |
| placeType | `RESTAURANT` | X | 장소 타입. `RESTAURANT`, `WALK` |

자동 동기화 동작

| **조건** | **동작** |
| --- | --- |
| `areaCode` 없음 | 전체 DB 장소 조회. 자동 동기화 없음 |
| `placeType` 없음 + 조회 결과 없음 | 음식점과 산책 코스 모두 자동 동기화 후 재조회 |
| `placeType=RESTAURANT` + 조회 결과 없음 | 음식점만 자동 동기화 후 재조회 |
| `placeType=WALK` + 조회 결과 없음 | 산책 코스만 자동 동기화 후 재조회 |
| 자동 동기화 실패 | 실패 로그만 남기고 기존 DB 조회 결과 반환 |

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
      "contentId": "2850913",
      "contentTypeId": "39",
      "placeType": "RESTAURANT",
      "title": "해녀의 부엌",
      "addr1": "제주특별자치도 제주시 성산읍 10-32",
      "addr2": "",
      "areaCode": "39",
      "sigunguCode": "4",
      "mapX": 126.1234567890,
      "mapY": 33.1234567890,
      "tel": "064-000-0000",
      "firstImage": "https://example.com/image.jpg",
      "firstImage2": "https://example.com/thumb.jpg",
      "cat1": "A05",
      "cat2": "A0502",
      "cat3": "A05020100",
      "aiRecommendationPoint": null,
      "menus": []
    }
  ]
}
```

응답 필드 설명

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| isSuccess | Boolean | 요청 성공 여부 |
| code | String | 응답 코드 |
| message | String | 응답 메시지 |
| result[].placeId | Long | 장소 ID |
| result[].contentId | String | TourAPI contentId |
| result[].contentTypeId | String | TourAPI contentTypeId |
| result[].placeType | Enum | `RESTAURANT`, `WALK` |
| result[].title | String | 장소 이름 |
| result[].addr1 | String | 기본 주소 |
| result[].addr2 | String | 상세 주소 |
| result[].areaCode | String | TourAPI 지역 코드 |
| result[].sigunguCode | String | TourAPI 시군구 코드 |
| result[].mapX | Decimal | 경도 |
| result[].mapY | Decimal | 위도 |
| result[].tel | String | 전화번호 |
| result[].firstImage | String | 대표 이미지 |
| result[].firstImage2 | String | 썸네일 이미지 |
| result[].cat1 | String | TourAPI 대분류 |
| result[].cat2 | String | TourAPI 중분류 |
| result[].cat3 | String | TourAPI 소분류 |
| result[].aiRecommendationPoint | String | 목록 조회에서는 `null` |
| result[].menus | Array | 목록 조회에서는 빈 배열 |

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "COMMON_400",
  "message": "검증 실패 메시지",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `placeType` enum 값 오류 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | DB 오류 등 내부 오류 |

---

# 3. 장소 상세 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/places/{placeId}` |
| Description | 장소 상세 정보와 메뉴 목록을 조회합니다. 음식점이면 TourAPI 상세 메뉴를 동기화한 뒤 메뉴 목록을 반환합니다. |
| Auth | 불필요 |
| Note | `placeId`는 DB에 존재하는 장소 ID입니다. |

---

## 2. Request

### 2-1. Header

없음

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| placeId | `1` | O | 장소 ID |

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
    "placeId": 1,
    "contentId": "2850913",
    "contentTypeId": "39",
    "placeType": "RESTAURANT",
    "title": "해녀의 부엌",
    "addr1": "제주특별자치도 제주시 성산읍 10-32",
    "addr2": "",
    "areaCode": "39",
    "sigunguCode": "4",
    "mapX": 126.1234567890,
    "mapY": 33.1234567890,
    "tel": "064-000-0000",
    "firstImage": "https://example.com/image.jpg",
    "firstImage2": "https://example.com/thumb.jpg",
    "cat1": "A05",
    "cat2": "A0502",
    "cat3": "A05020100",
    "aiRecommendationPoint": "식사 전후 동선에 넣기 좋은 장소입니다.",
    "menus": [
      {
        "menuId": 1,
        "placeId": 1,
        "name": "전복죽",
        "menuType": "MEAL",
        "calories": 450,
        "sodium": 1100,
        "sugar": 15
      }
    ]
  }
}
```

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "PLACE_404",
  "message": "장소를 찾을 수 없습니다.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `placeId` 타입 오류 |
| 404 | PLACE_404 | 장소를 찾을 수 없습니다. | 존재하지 않는 장소 ID |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 4. 추천 메뉴 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/trips/{tripId}/recommendations/menus` |
| Description | 여행 지역 음식점 메뉴를 사용자 건강 상태와 오늘 섭취량 기준으로 추천합니다. |
| Auth | 필요 |
| Note | 여행 지역 장소 또는 메뉴가 없으면 음식점 동기화를 자동 시도한 뒤 추천합니다. 자동 동기화 실패 시 기존 DB 결과 기준으로 응답합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `1` | O | 여행 ID |

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
      "placeName": "해녀의 부엌",
      "menuId": 1,
      "menuName": "전복죽",
      "menuType": "MEAL",
      "score": 92,
      "level": "EXCELLENT",
      "reasons": [
        {
          "type": "SODIUM",
          "message": "나트륨이 오늘 남은 기준을 초과합니다.",
          "penalty": 20,
          "exceededAmount": 150
        }
      ]
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
| result[].reasons | Array | 감점 사유 목록 |
| result[].reasons[].type | Enum | `CALORIE`, `SODIUM`, `SUGAR` |
| result[].reasons[].message | String | 감점 사유 메시지 |
| result[].reasons[].penalty | Integer | 감점 점수 |
| result[].reasons[].exceededAmount | Integer | 초과량 |

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "TRIP_404",
  "message": "Trip not found.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `tripId` 타입 오류 |
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
| Note | 내부적으로 추천 메뉴 목록을 사용합니다. 장소 또는 메뉴가 없으면 음식점 동기화를 자동 시도합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `1` | O | 여행 ID |

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
      "placeName": "해녀의 부엌",
      "addr1": "제주시 성산읍 10-32",
      "mapX": 126.1234567890,
      "mapY": 33.1234567890,
      "firstImage": "https://example.com/image.jpg",
      "bestMenu": {
        "placeId": 1,
        "placeName": "해녀의 부엌",
        "menuId": 1,
        "menuName": "전복죽",
        "menuType": "MEAL",
        "score": 92,
        "level": "EXCELLENT",
        "reasons": [
          {
            "type": "SODIUM",
            "message": "나트륨이 오늘 남은 기준을 초과합니다.",
            "penalty": 20,
            "exceededAmount": 150
          }
        ]
      },
      "aiCourseMessage": "식사 전후 동선에 넣기 좋은 장소로, 메뉴 선택 시 나트륨과 당류를 함께 확인해보세요."
    }
  ]
}
```

주의: `bestMenu.recommendationMessage`는 현재 이 응답에 포함되지 않습니다.

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "TRIP_404",
  "message": "Trip not found.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `tripId` 타입 오류 |
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
| Note | 디저트 메뉴가 없으면 음식점 동기화를 자동 시도합니다. TourAPI 메뉴명에 디저트/음료 키워드가 포함되면 `DESSERT`로 자동 분류됩니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `1` | O | 여행 ID |

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
      "placeId": 2,
      "placeName": "제주 카페",
      "menuId": 10,
      "menuName": "Cafe Latte",
      "menuType": "DESSERT",
      "score": 95,
      "level": "EXCELLENT",
      "reasons": []
    }
  ]
}
```

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "TRIP_404",
  "message": "Trip not found.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `tripId` 타입 오류 |
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
| Description | 여행 지역의 산책 코스 후보를 추천합니다. |
| Auth | 필요 |
| Note | 산책 코스가 없으면 산책 코스 동기화를 자동 시도한 뒤 조회합니다. 자동 동기화 실패 시 기존 DB 결과 기준으로 응답합니다. |

---

## 2. Request

### 2-1. Header

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| Authorization | `Bearer {accessToken}` | O | JWT Access Token |

### 2-2. Path Variable

| **Key** | **Value (Example)** | **Required** | **Description** |
| --- | --- | --- | --- |
| tripId | `1` | O | 여행 ID |

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
      "placeName": "가마오름",
      "addr1": "제주특별자치도 제주시 한경면 청수리",
      "mapX": 126.2466098987,
      "mapY": 33.3057279944,
      "firstImage": "https://example.com/image.jpg"
    }
  ]
}
```

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "TRIP_404",
  "message": "Trip not found.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `tripId` 타입 오류 |
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

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "HEALTH_404",
  "message": "건강 프로필을 찾을 수 없습니다.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | HEALTH_404 | 건강 프로필을 찾을 수 없습니다. | 건강 프로필 생성 필요 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 9. 오늘 식사 기록 목록 조회

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/meal-records/today` |
| Description | 오늘 등록된 식사 기록 목록을 조회합니다. |
| Auth | 필요 |
| Note | 영양 요약 상세 내역 화면에서 사용할 수 있습니다. |

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
  "result": [
    {
      "mealRecordId": 2,
      "menuId": 266,
      "menuName": "전복죽",
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

```json
{
  "isSuccess": false,
  "code": "COMMON_401",
  "message": "인증이 필요합니다.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
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

### 2-2. Path Variable

없음

### 2-3. Query String

없음

### 2-4. Request Body

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
    "menuName": "전복죽",
    "mealType": "LUNCH",
    "eatenAt": "2026-09-20T12:00:00",
    "calories": 450,
    "sodium": 1100,
    "sugar": 15
  }
}
```

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "MEAL_404",
  "message": "메뉴를 찾을 수 없습니다.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | 필수값 누락 또는 enum 값 오류 |
| 400 | MEAL_400 | 유효하지 않은 식사 시간입니다. | `eatenAt`이 현재보다 미래인 경우 |
| 401 | COMMON_401 | 인증이 필요합니다. | JWT 누락 또는 유효하지 않은 토큰 |
| 404 | MEAL_404 | 메뉴를 찾을 수 없습니다. | 존재하지 않는 메뉴 ID |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | 서버 내부 로직 오류 |

---

# 11. 식사 완료

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/meal-records/complete` |
| Description | 추천 메뉴를 먹은 것으로 기록하고, 갱신된 오늘 영양 요약과 후속 추천 가능 여부를 반환합니다. |
| Auth | 필요 |
| Note | 프론트의 `먹었어요`, `식사 완료` 버튼에 연결합니다. |

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
      "menuName": "전복죽",
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

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "TRIP_404",
  "message": "Trip not found.",
  "result": null
}
```

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

# 12. 운영용 음식점 동기화

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/admin/places/sync/restaurants` |
| Description | TourAPI 음식점 데이터를 서버 DB에 동기화합니다. 음식점 저장 후 메뉴도 함께 동기화합니다. |
| Auth | Admin Secret 필요 |
| Note | 프론트 연동 대상이 아닙니다. 운영자가 데이터를 미리 채워 첫 조회 지연을 줄일 때 사용합니다. |

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
| pageNo | `1` | X | 페이지 번호. 기본값 `1` |
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

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "COMMON_403",
  "message": "접근 권한이 없습니다.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 403 | COMMON_403 | 접근 권한이 없습니다. | `X-Admin-Secret` 누락, 불일치, 또는 `ADMIN_SYNC_SECRET` 미설정 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | TourAPI 키 오류, 외부 API 장애, DB 오류 |

---

# 13. 운영용 산책 코스 동기화

## 1. API 개요

| **분류** | **내용** |
| --- | --- |
| Method | POST |
| URL | `/api/admin/places/sync/walks` |
| Description | TourAPI 자연 관광지 데이터를 산책 코스 후보로 서버 DB에 동기화합니다. |
| Auth | Admin Secret 필요 |
| Note | 프론트 연동 대상이 아닙니다. 운영자가 데이터를 미리 채워 첫 조회 지연을 줄일 때 사용합니다. |

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
| pageNo | `1` | X | 페이지 번호. 기본값 `1` |
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
    "fetchedCount": 94,
    "createdCount": 92,
    "updatedCount": 2
  }
}
```

### 3-2. Error Response

```json
{
  "isSuccess": false,
  "code": "COMMON_403",
  "message": "접근 권한이 없습니다.",
  "result": null
}
```

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 403 | COMMON_403 | 접근 권한이 없습니다. | `X-Admin-Secret` 누락, 불일치, 또는 `ADMIN_SYNC_SECRET` 미설정 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | TourAPI 키 오류, 외부 API 장애, DB 오류 |
