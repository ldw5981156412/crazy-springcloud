--- -1 failed
--- 1 success

local key = KEYS[1]
local requestId = KEYS[2]   --- 身份标识
local value = redis.call('get',key)
if value == requestId then
    redis.call('del',key);
    return 1;
end
return -1