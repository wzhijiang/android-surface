//
// Created by 王志江 on 9/15/21.
//

#ifndef ANDROIDSURFACE_LOGGER_H
#define ANDROIDSURFACE_LOGGER_H

#include <android/log.h>

#define LOGD(category, ...) \
  ((void)__android_log_print(ANDROID_LOG_DEBUG, category, __VA_ARGS__))

#undef LOGE
#define LOGE(category, ...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, category, __VA_ARGS__))

#undef LOGI
#define LOGI(category, ...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, category, __VA_ARGS__))

#undef LOGW
#define LOGW(category, ...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, category, __VA_ARGS__))

#endif //ANDROIDSURFACE_LOGGER_H
