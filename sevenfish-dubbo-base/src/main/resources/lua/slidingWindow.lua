local key = KEYS[1]
-- 一个时间窗口限制数量
local limitCount = tonumber(ARGV[1])
-- 获取当前时间，单位毫秒
local currentTime = tonumber(ARGV[2])
-- 当前时间戳的value值，唯一
local value = ARGV[3]
-- 获取时间窗口范围，传参单位秒，默认窗口一秒
local timeRange
if ARGV[4] == nil then
	timeRange = 1000
else
	timeRange = tonumber(ARGV[4]) * 1000
end
-- 获取集合key过期时间，当key过期时会存在瞬时并发的情况，因此过期时间不能太短或者改用定时清除，传参单位秒，默认一小时
local expiration
if ARGV[5] == nil then
	expiration = 3600
else
	expiration = tonumber(ARGV[5])
end
-- 前一秒内已访问的次数
local beforeCount = 0
local existKey = redis.call('exists', key)
if (existKey == 1) then
	-- 计算前一秒访问次数
	beforeCount = redis.call('zcount', key, currentTime - timeRange, currentTime)
end
local result = 0
if (limitCount > beforeCount) then
	result = 1
	-- 记录当前访问时间
    redis.call('zadd', key, currentTime, value)
	-- 设置过期时间
    redis.call('expire', key, expiration)
end
return result