fun main() {
    val dataMap = mapOf("temp" to 23, "humi" to 42)
    println(dataMap)
    
    val t = dataMap["temp"] // or .get("temp")
    println(t)
    
    val keySet = dataMap.keys;
    println(keySet)
    
    val mutableMap = dataMap.toMutableMap()
    mutableMap["temp"] = 5 // or .put("temp", 5)
    println(mutableMap)
}