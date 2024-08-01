#当前包名&当前百度sdk key  %不要修改
cur_package="com.huolala.mockgps"
cur_key="lmSE5lD0ZXcSAuNs34A2DXQdIzaDI8GR"

#百度开发者平台申请key 需要的信息  开发版SHA1和发布版SHA1相同即可
发布版SHA1="7B:11:FA:A1:1E:F9:A8:85:76:D1:FD:79:D7:66:50:99:5E:3A:D4:0D"

#替换包名&百度sdk key <***>随意修改  %需要上百度sdk开发者平台先申请key
replace_package="com.***.***"
replace_key="***"


sed -i '' 's/'${cur_package}'/'${replace_package}'/g' config.gradle
sed -i '' 's/'${cur_key}'/'${replace_key}'/g' config.gradle
./gradlew app:assembleRelease
sed -i '' 's/'${replace_package}'/'${cur_package}'/g' config.gradle
sed -i '' 's/'${replace_key}'/'${cur_key}'/g' config.gradle
open app/build/outputs/apk/release/
