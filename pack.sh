#当前包名&当前百度sdk key  %不要修改
cur_package="com.huolala.mockgps"
cur_key="lmSE5lD0ZXcSAuNs34A2DXQdIzaDI8GR"


#替换包名&百度sdk key <***>随意修改  %需要上百度sdk开发者平台先申请key
replace_package="com.***.***"
replace_key="***"


sed -i '' 's/'${cur_package}'/'${replace_package}'/g' config.gradle
sed -i '' 's/'${cur_key}'/'${replace_key}'/g' config.gradle
./gradlew app:assembleRelease
sed -i '' 's/'${replace_package}'/'${cur_package}'/g' config.gradle
sed -i '' 's/'${replace_key}'/'${cur_key}'/g' config.gradle
open app/build/outputs/apk/release/
