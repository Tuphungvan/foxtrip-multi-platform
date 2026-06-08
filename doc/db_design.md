[ER Diagram](/picture/database.png)

#### 1. User

Table users {
  id            uuid      [pk]
  username      varchar   [not null]
  email         varchar   [unique, not null]
  password_hash varchar
  phone_number  varchar(10) [unique, format 10 so']
  province      varchar(50) [enum]
  role          varchar(50) [not null, default: 'USER']
  is_verified   boolean   [not null, default: false]
  avatar_url    varchar(512) [not null, default: 'https://res.cloudinary.com/do1ill8ba/image/upload/v1775034651/default_image.png']
  google_id     varchar   [unique]
  deleted_at    timestamp
  created_at    timestamp [not null, default: `now()`]
  updated_at    timestamp [not null, default: `now()`]
  created_by    varchar
  updated_by    varchar

  indexes {
    (role, deleted_at) [name: 'idx_users_role_active', type: btree, note: 'App cần lọc nhanh danh sách User theo Role và trạng thái hoạt động']
    province [name: 'idx_users_province', type: btree, note: 'App bắt buộc có tính năng tìm kiếm user theo Province để phục vụ phân công Guide']
    username [name: 'idx_users_username_trgm', type: gin, note: 'App yêu cầu tìm kiếm mờ (fuzzy search) User theo Username']
    email [name: 'idx_users_email_trgm', type: gin, note: 'App yêu cầu tìm kiếm mờ (fuzzy search) User theo Email']
    email [unique, name: 'uk_users_email', type: btree, note: 'Email là duy nhất, dùng để định danh và đăng nhập']
    phone_number [unique, name: 'uk_users_phone_number', type: btree, note: 'Số điện thoại là duy nhất, phục vụ bảo mật và liên lạc']
    google_id [unique, name: 'uk_users_google_id', type: btree, note: 'Google ID là duy nhất, dùng cho xác thực Social Login']
  }
}

Enum user_role {
  USER
  GUIDE
  ADMIN
  SUPER_ADMIN
}

#### 2. Tour

Table tours {
  id              uuid          [pk]
  name            varchar       [not null]
  slug            varchar       [unique, not null]
  description     text          [not null]
  province        varchar(50)   [not null]
  category        tour_category [not null]
  short_id        varchar       [unique, not null]
  thumbnail_url   varchar       [not null, note: 'anh dai dien tour; co the sync/snapshot tu anh location']
  price           decimal(15,2) [not null]
  discount        decimal(15,2) [not null, default: 0]
  slots           int           [not null]
  available_slots int           [not null, note: 'Bắt buộc cập nhật Atomic: available_slots = available_slots - quantity']
  guide_id        uuid
  start_date      timestamp     [not null]
  end_date        timestamp     [not null]
  status          tour_status   [not null, default: 'HIDDEN']
  review_count    int           [not null, default: 0]
  average_rating  decimal(3,2)  [note: 'null khi chưa có review']
  deleted_at      timestamp
  created_at      timestamp     [not null, default: `now()`]
  updated_at      timestamp     [not null, default: `now()`]
  created_by      varchar
  updated_by      varchar 
  setup_step      tour_step_status [not null]

  indexes {
    slug [unique, name: 'idx_tours_slug', type: btree, note: 'Tối ưu định danh tour trên URL']
    name [name: 'idx_tours_name_trgm', type: gin, note: 'Tìm kiếm mờ tour theo tên']
    status [name: 'idx_tours_status', type: btree, note: 'Lọc nhanh tour theo trạng thái quản lý']
    start_date [name: 'idx_tours_start_date', type: btree, note: 'Lọc tour dự kiến và sắp khởi hành']
    end_date [name: 'idx_tours_end_date', type: btree, note: 'Job kết thúc tour sử dụng cột này']
    province [name: 'idx_tours_province', type: btree, note: 'Lọc tour theo tỉnh thành']
    category [name: 'idx_tours_category', type: btree, note: 'Lọc tour theo danh mục']
    price [name: 'idx_tours_price', type: btree, note: 'Bộ lọc khoảng giá tour']
    (guide_id, start_date, end_date) [name: 'idx_tours_guide_schedule', type: btree, note: 'Kiểm tra trùng lịch Guide']
    short_id [unique, name: 'uk_tours_short_id', type: btree, note: 'Mã định danh rút gọn']
    available_slots [name: 'idx_tours_available_slots', type: btree, note: 'Lọc nhanh các tour còn chỗ']
    deleted_at [name: 'idx_tours_deleted_at', type: btree, note: 'Tối ưu cho truy vấn soft-delete']
  }
}

Enum tour_category {
  SEA
  CULTURE
  NATURE
  RELAX
}

Enum tour_status {
  HIDDEN
  ACTIVE
  ONGOING
  COMPLETED
}

Enum tour_step_status {
BASIC_DONE,
ITINERARY_DONE,
ADDON_DONE,
READY
}

#### 3. Tour Detail (Itinerary & Addon)

Table tour_itineraries {
  id          uuid      [pk]
  tour_id     uuid      [not null]
  location_id uuid      // null cho bước không gắn location
  day_number  int       [not null]
  position    int       [not null]
  activity    text      [not null]
  created_at  timestamp [not null, default: `now()`]
  updated_at  timestamp [not null, default: `now()`]
  created_by  varchar
  updated_by  varchar

  indexes {
    (tour_id, day_number, position) [unique, name: 'uk_itinerary_order', type: btree, note: 'Quản lý thứ tự các điểm tham quan trong một tour']
    location_id [name: 'idx_itinerary_location_id', type: btree, note: 'Tối ưu hiệu năng khi xóa địa điểm hoặc tìm tour theo địa danh']
  }
}

Table tour_addons {
  id          uuid          [pk]
  tour_id     uuid          [not null]
  location_id uuid
  name        varchar       [not null]
  description text          [not null]
  price       decimal(15,2) [not null]
  is_active   boolean       [not null, default: true]
  created_at  timestamp     [not null, default: `now()`]
  updated_at  timestamp     [not null, default: `now()`]
  created_by  varchar
  updated_by  varchar

  indexes {
    (tour_id, name) [unique, name: 'uk_tour_addons_tour_name', type: btree, note: 'Mỗi tour không được trùng lặp tên Add-on']
    tour_id [name: 'idx_tour_addons_tour_id', type: btree, note: 'Lấy nhanh danh sách Add-on của một tour']
  }
}

#### 4. Location

Table locations {
  id             uuid        [pk]
  name           varchar     [not null]
  province       varchar(50) [not null]
  address        text        [not null]
  coordinates    geometry    [not null] // Point(4326)
  mapbox_place_id varchar    [unique, note: 'optional; place id tu Mapbox de dinh danh/chong duplicate']
  type           loc_type    [not null, default: 'OTHER']
  priority       int         [not null, default: 0, note: 'priority > 0 la location dang thue quang cao']
  featured_start_at timestamp [note: 'thoi gian bat dau quang cao (UTC)']
  featured_end_at   timestamp [note: 'thoi gian het han quang cao (UTC)']
  contact_phone  varchar
  image_url      varchar     [not null, note: 'anh dai dien duy nhat cua location']
  deleted_at     timestamp
  created_at     timestamp [not null, default: `now()`]
  updated_at     timestamp [not null, default: `now()`]
  created_by     varchar
  updated_by     varchar

  indexes {
    name [name: 'idx_locations_name_trgm', type: gin, note: 'Hệ thống yêu cầu tìm kiếm mờ (fuzzy search) địa điểm theo tên']
    address [name: 'idx_locations_address_trgm', type: gin, note: 'Hỗ trợ Smart Search tìm kiếm theo địa chỉ chi tiết']
    province [name: 'idx_locations_province_trgm', type: gin, note: 'Hỗ trợ Smart Search nhận diện tỉnh thành trong ô search chung']
    type [name: 'idx_locations_type', type: btree, note: 'Tối ưu lọc theo loại hình (Cafe, Hotel...) từ kết quả Mapper']
    (coordinates) [name: 'idx_locations_geom_gist', type: gist, note: 'Phục vụ truy vấn không gian trên Bản đồ (tìm địa điểm lân cận)']
    mapbox_place_id [unique, name: 'uk_locations_mapbox_place', type: btree, note: 'Chống trùng lặp địa điểm khi đồng bộ từ Mapbox']
    priority [name: 'idx_locations_priority_desc', type: btree, note: 'Dùng để xếp hạng (Ranking) - Đẩy địa điểm Ads lên đầu kết quả tìm kiếm']
    (name, province, address) [unique, name: 'uk_locations_identity', type: btree, note: 'Chống trùng lặp địa điểm khi không có mã Mapbox']
  }
}

Enum loc_type {
  CAFE
  HOTEL
  RESORT
  HOMESTAY
  RESTAURANT
  FOOD
  ATTRACTION
  MUSEUM
  PARK
  OTHER
}

#### 5. Order

Table orders {
  id              uuid          [pk]
  user_id         uuid          [not null]
  order_code      varchar       [unique, not null]
  customer_name   varchar       [not null]
  customer_phone  varchar       [not null]
  customer_email  varchar       [not null]
  total_amount    decimal(15,2) [not null]
  status          order_status  [not null, default: 'PENDING']
  status_note     text          [note: 'ghi lý do nội bộ, ví dụ LATE_IPN_ALLOW_PAID / EXPIRED_AUTO']
  vnpay_txn_ref   varchar
  paid_at         timestamp
  expires_at      timestamp     [not null, note: 'created_at + 24h']
  created_at      timestamp     [not null, default: `now()`]
  updated_at      timestamp     [not null, default: `now()`]
  created_by      varchar
  updated_by      varchar

  indexes {
    (user_id, created_at) [name: 'idx_orders_user_time', type: btree, note: 'Lấy lịch sử đơn hàng của User theo thời gian']
    (status, created_at) [name: 'idx_orders_status_time', type: btree, note: 'Thống kê và lọc đơn hàng theo trạng thái']
    order_code [unique, name: 'uk_orders_code', type: btree, note: 'Mã đơn hàng là duy nhất dùng để tra cứu và thanh toán']
    expires_at [name: 'idx_orders_expires_at', type: btree, note: 'Tối ưu cho Job quét hủy đơn quá hạn chưa thanh toán']
  }
}

Enum order_status {
  PENDING
  PAID
  CANCEL_REQUESTED
  REFUND_PENDING
  REFUNDED
  COMPLETED
  EXPIRED
}

Table order_items {
  id                     uuid          [pk]
  order_id               uuid          [not null]
  tour_id                uuid          [not null]
  quantity               int           [not null]
  tour_name_at_time      varchar       [not null]
  start_date_at_time     timestamp     [not null]
  end_date_at_time       timestamp     [not null]
  thumbnail_at_time      varchar       [not null]
  price_at_time          decimal(15,2) [not null]
  discount_at_time       decimal(15,2) [not null, default: 0]
  final_price            decimal(15,2) [not null]
  created_at             timestamp     [not null, default: `now()`]
  updated_at             timestamp     [not null, default: `now()`]
  created_by             varchar
  updated_by             varchar

  indexes {
    order_id [name: 'idx_order_items_order', type: btree, note: 'Lấy chi tiết danh sách tour trong một đơn hàng']
    tour_id [name: 'idx_order_items_tour', type: btree, note: 'Thống kê số lượng đặt của một tour cụ thể']
    (order_id, tour_id) [name: 'idx_order_items_checkin', type: btree, note: 'Xác minh check-in nhanh: đơn hàng này có thuộc tour này không']
  }
}

Table order_addons {
  id            uuid          [pk]
  order_id      uuid          [not null]
  tour_addon_id uuid          [not null]
  quantity      int           [not null, default: 1]
  price_at_time decimal(15,2) [not null]
  created_at    timestamp     [not null, default: `now()`]
  updated_at    timestamp     [not null, default: `now()`]
  created_by    varchar
  updated_by    varchar

  indexes {
    order_id [name: 'idx_order_addons_order', type: btree, note: 'Lấy danh sách Add-on đã mua trong một đơn hàng']
  }
}

#### 6. Refund

Table refunds {
  id            uuid           [pk]
  order_id      uuid           [not null]
  amount        decimal(15,2)  [not null]
  status        refund_status  [not null, default: 'PENDING']
  reason        text           [not null]
  processed_by  uuid           [note: 'ID cua Admin/Super Admin xu ly refund']
  processed_at  timestamp
  created_at    timestamp      [not null, default: `now()`]
  updated_at    timestamp      [not null, default: `now()`]
  created_by    varchar
  updated_by    varchar

  indexes {
    order_id [name: 'idx_refunds_order', type: btree, note: 'Tra cứu thông tin hoàn tiền gắn với đơn hàng']
    processed_by [name: 'idx_refunds_processor', type: btree, note: 'Audit: xem danh sách các lệnh hoàn tiền đã xử lý bởi một Admin']
  }
}

Enum refund_status {
  PENDING
  COMPLETED
  FAILED
}

#### 7. Cart

Table carts {
  id          uuid      [pk]
  user_id     uuid      [unique, not null]
  created_at  timestamp [not null, default: `now()`]
  updated_at  timestamp [not null, default: `now()`]
  created_by  varchar
  updated_by  varchar

  indexes {
    user_id [unique, name: 'uk_carts_user_id', type: btree, note: 'Mỗi User chỉ có một Giỏ hàng duy nhất']
  }
}

Table cart_items {
  id          uuid      [pk]
  cart_id     uuid      [not null]
  tour_id     uuid      [not null]
  quantity    int       [not null, default: 1]
  created_at  timestamp [not null, default: `now()`]
  updated_at  timestamp [not null, default: `now()`]
  created_by  varchar
  updated_by  varchar

  indexes {
    cart_id [name: 'idx_cart_items_cart', type: btree, note: 'Tối ưu filter theo giỏ hàng. Quy trình nghiệp vụ luôn JOIN cart_items sang tours qua PK của tours (đã index sẵn), nên không cần index tour_id trong scope này. Hệ thống không lưu trữ/truy vấn thống kê theo tour_id tại bảng này.']
  }
}

#### 8. Review

Table reviews {
  id            uuid      [pk]
  user_id       uuid      [not null]
  tour_id       uuid      [not null]
  rating        int       [not null]
  content       text
  edited_at     timestamp
  created_at    timestamp [not null, default: `now()`]
  updated_at    timestamp [not null, default: `now()`]
  created_by    varchar
  updated_by    varchar

  indexes {
    (user_id, tour_id) [unique, name: 'uk_reviews_user_tour', type: btree, note: 'Một user chỉ được đánh giá một tour một lần']
    (tour_id, created_at) [name: 'idx_reviews_tour_time', type: btree, note: 'Lấy các review của tour sắp xếp theo thời gian mới nhất']
  }
}

#### 9. Revenue

Table revenue_reports {
  id            uuid          [pk]
  month         int           [not null]
  year          int           [not null]
  revenue_tour   decimal(15,2) [not null, default: 0]
  revenue_addon  decimal(15,2) [not null, default: 0]
  revenue_ads    decimal(15,2) [not null, default: 0]
  total_revenue decimal(15,2) [not null, default: 0]
  total_orders  int           [not null, default: 0]
  created_at    timestamp     [not null, default: `now()`]
  updated_at    timestamp     [not null, default: `now()`]
  created_by    varchar
  updated_by    varchar

  indexes {
    (year, month) [unique, name: 'uk_revenue_period', type: btree, note: 'Đảm bảo mỗi tháng/năm chỉ có một bản báo cáo doanh thu']
  }
}

#### 10. Ref (Foreign Keys)

Ref: tours.guide_id > users.id [delete: set null]
Ref: tour_itineraries.tour_id > tours.id [delete: restrict]
Ref: tour_itineraries.location_id > locations.id [delete: set null]
Ref: tour_addons.tour_id > tours.id [delete: restrict]
Ref: tour_addons.location_id > locations.id [delete: set null]
Ref: carts.user_id > users.id [delete: restrict]
Ref: cart_items.cart_id > carts.id [delete: restrict]
Ref: cart_items.tour_id > tours.id [delete: restrict]
Ref: orders.user_id > users.id [delete: set null]
Ref: order_items.order_id > orders.id [delete: restrict]
Ref: order_items.tour_id > tours.id [delete: restrict]
Ref: order_addons.order_id > orders.id [delete: restrict]
Ref: order_addons.tour_addon_id > tour_addons.id [delete: restrict]
Ref: refunds.order_id > orders.id [delete: restrict]
Ref: refunds.processed_by > users.id [delete: set null]
Ref: reviews.user_id > users.id [delete: restrict]
Ref: reviews.tour_id > tours.id [delete: restrict]
