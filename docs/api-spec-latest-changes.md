# Latest API spec changes

## 1. 사용자용 장소 목록 조회

| **분류** | **내용** |
| --- | --- |
| Method | GET |
| URL | `/api/places` |
| Description | 지역 코드와 장소 타입 조건으로 장소 목록을 조회합니다. 조회 결과가 없으면 필요한 TourAPI 동기화를 자동 시도한 뒤 다시 조회합니다. |
| Auth | 불필요 |
| Note | `areaCode`가 없으면 전체 DB 조회만 수행하고 자동 동기화는 하지 않습니다. 자동 동기화 실패 시 서버 에러로 터뜨리지 않고 기존 DB 조회 결과를 반환합니다. |

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
| `areaCode` 없음 | 전체 DB 장소 조회. 자동 동기화하지 않음 |
| `placeType` 없음 + 조회 결과 없음 | 음식점과 산책 코스를 모두 자동 동기화 후 재조회 |
| `placeType=RESTAURANT` + 조회 결과 없음 | 음식점만 자동 동기화 후 재조회 |
| `placeType=WALK` + 조회 결과 없음 | 산책 코스만 자동 동기화 후 재조회 |
| 자동 동기화 실패 | 실패 로그만 남기고 기존 조회 결과 반환 |

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
      "title": "제주 식당",
      "addr1": "제주특별자치도 제주시 ...",
      "addr2": "",
      "areaCode": "39",
      "sigunguCode": "4",
      "mapX": 126.5312,
      "mapY": 33.4996,
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

| **HTTP Status** | **Error Code** | **Message** | **Cause & Solution** |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 검증 실패 메시지 | `placeType` enum 값 오류 |
| 500 | COMMON_500 | 서버 내부 오류가 발생했습니다. | DB 오류 등 내부 오류 |

---

# 추천 API 자동 동기화 변경

## 1. 대상 API

| **Method** | **URL** | **자동 동기화** |
| --- | --- | --- |
| GET | `/api/trips/{tripId}/recommendations/menus` | 음식점 또는 메뉴가 없으면 음식점 동기화 자동 시도 |
| GET | `/api/trips/{tripId}/recommendations/places` | 내부적으로 메뉴 추천을 사용하므로 음식점 동기화 자동 시도 |
| GET | `/api/trips/{tripId}/recommendations/desserts` | 디저트 메뉴가 없으면 음식점 동기화 자동 시도 |
| GET | `/api/trips/{tripId}/recommendations/walks` | 산책 코스가 없으면 산책 코스 동기화 자동 시도 |

## 2. 변경된 Note

| **API** | **Note** |
| --- | --- |
| `/recommendations/menus` | 수동 동기화가 없어도 첫 조회 시 음식점 동기화를 자동 시도합니다. |
| `/recommendations/places` | 장소별 최고 메뉴 추천 기반입니다. 메뉴가 없으면 음식점 동기화를 자동 시도합니다. |
| `/recommendations/desserts` | TourAPI 메뉴명에 디저트/음료 키워드가 포함되면 `DESSERT`로 자동 분류됩니다. |
| `/recommendations/walks` | 산책 코스가 없으면 산책 코스 동기화를 자동 시도합니다. |

## 3. 자동 동기화 실패 처리

| **상황** | **동작** |
| --- | --- |
| TourAPI 장애 | 실패 로그를 남기고 기존 DB 결과로 응답 |
| TourAPI 키 오류 | 실패 로그를 남기고 기존 DB 결과로 응답 |
| 기존 DB 데이터도 없음 | 빈 배열 응답 |

---

# 디저트 메뉴 자동 분류

## 1. 개요

| **분류** | **내용** |
| --- | --- |
| 대상 | TourAPI 음식점 상세 메뉴 동기화 |
| Description | 메뉴명이 디저트/음료 키워드를 포함하면 `MenuType.DESSERT`로 저장합니다. 그 외는 `MEAL`로 저장합니다. |
| Note | 기존에 `MEAL`로 저장된 메뉴도 다음 음식점 동기화 때 디저트 키워드에 해당하면 `DESSERT`로 재분류됩니다. |

## 2. 예시

| **Menu Name** | **Saved menuType** |
| --- | --- |
| `고기국수` | `MEAL` |
| `Cafe Latte` | `DESSERT` |
| `아이스크림` | `DESSERT` |
| `케이크` | `DESSERT` |
| `커피` | `DESSERT` |

---

# 운영용 수동 동기화 API 역할 변경

## 1. API

```http
POST /api/admin/places/sync/restaurants
POST /api/admin/places/sync/walks
```

## 2. 변경된 의미

| **Before** | **After** |
| --- | --- |
| 추천/장소 조회 전에 운영자가 지역별로 미리 호출해야 하는 API | 첫 조회 지연을 줄이기 위해 운영자가 미리 데이터를 채우는 선택 API |

사용자용 장소/추천 API에 자동 동기화가 들어갔기 때문에, 운영자가 모든 지역을 수동 동기화하지 않아도 기본 기능은 동작합니다.
