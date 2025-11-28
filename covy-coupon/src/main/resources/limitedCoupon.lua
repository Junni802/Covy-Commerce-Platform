-- limited_coupon.lua

-- 이미 쿠폰을 받은 사용자일 경우

if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
  return 0
end

-- 쿠폰 수량 증가
local count = redis.call("INCR", KEYS[2])

-- 수량이 초과되었으면 되돌리고 종료
if (count > 15) then
  redis.call('DECR', KEYS[2])
  return -1
end

-- 사용자 등록
redis.call('SADD', KEYS[1], ARGV[1])
return 1