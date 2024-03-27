package com.example.wifimeasurement

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Composable
fun WifiMeasureReport (positionName: String?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var wifiScanResult by remember { mutableStateOf<List<WifiMeasureData>>(listOf(WifiMeasureData("", 0))) }
    DisposableEffect(Unit) {
        val wifiScanService = WifiScanService(context)
        wifiScanService.init()

        val job = scope.launch {
            wifiScanService.data
                .receiveAsFlow()
                .onEach { wifiScanResult = it }
                .collect {}
        }

        onDispose {
            wifiScanService.cancel()
            job.cancel()
        }
    }

    Column() {
        Text(
            text = stringResource(id = R.string.report_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.report_position_name)+": "+positionName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(10.dp),
            color = MaterialTheme.colorScheme.secondary
        )
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ){
            item {
                ItemHeader()
            }
            itemsIndexed(wifiScanResult) { index: Int, item: WifiMeasureData ->
                ItemRow(index, item)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { postDataUsingRetrofit(
                context, positionName!!, 0.0f, wifiScanResult
            ) },
            shape = RectangleShape,
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        ) {
            Text(
                text = stringResource(R.string.upload),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ItemHeader() {
    Row(
        Modifier
            .fillMaxWidth()
        //.border(BorderStroke(0.5.dp, Color.Black))
    ) {
        Text(text = stringResource(R.string.report_header_bssid), fontWeight = FontWeight.Bold, modifier = Modifier
            .weight(5f)
            .padding(10.dp), color = MaterialTheme.colorScheme.secondary)
        Text(text = stringResource(R.string.report_header_strength), fontWeight = FontWeight.Bold, modifier = Modifier
            .weight(5f)
            .padding(10.dp), color = MaterialTheme.colorScheme.secondary)
    }
    Divider(
        color = Color.LightGray,
        modifier = Modifier
            .height(1.dp)
            .fillMaxHeight()
            .fillMaxWidth()
    )
}

@Composable
fun ItemRow(index: Int, item: WifiMeasureData) {
    val modifier: Modifier = Modifier.fillMaxWidth()
    Row(
        modifier = if (index%2 == 0) modifier.background(Color.LightGray) else modifier
    ) {
        Text(text = item.bssId, modifier = Modifier
            .weight(5f)
            .padding(10.dp))
        Text(text = item.signalStrength.toString(), modifier = Modifier
            .weight(5f)
            .padding(10.dp))
    }
    Divider(
        color = Color.LightGray,
        modifier = Modifier
            .height(1.dp)
            .fillMaxHeight()
            .fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMeasureReportScreen() {
    WifiMeasureReport("grid_1")
}

private fun postDataUsingRetrofit(
    ctx: Context,
    positionName: String,
    angle: Float,
    wifiScanResult: List<WifiMeasureData>
) {
    var url = "http://192.168.0.109:8080/"
    // on below line we are creating a retrofit
    // builder and passing our base url
    val gson = GsonBuilder().setLenient().create()
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        // as we are sending data in json format so
        // we have to add Gson converter factory
        .addConverterFactory(GsonConverterFactory.create(gson))
        // at last we are building our retrofit builder.
        .build()
    // below the line is to create an instance for our retrofit api class.
    val retrofitAPI = retrofit.create(RetrofitAPI::class.java)
    val measureData = WifiMeasureReportData(positionName, angle, wifiScanResult)
    // calling a method to create an update and passing our model class.
    val call: Call<WifiMeasureReportData?>? = retrofitAPI.postData(measureData)
    // on below line we are executing our method.
    call!!.enqueue(object : Callback<WifiMeasureReportData?> {
        override fun onResponse(
            call: Call<WifiMeasureReportData?>,
            response: Response<WifiMeasureReportData?>
        ) {
            // this method is called when we get response from our api.
            Toast.makeText(ctx, "成功上传，响应码:" + response.code(), Toast.LENGTH_SHORT).show()
            //Toast.makeText(ctx, "Response Code : " + response.code(), Toast.LENGTH_SHORT).show()
            // we are getting a response from our body and
            // passing it to our model class.
            //val model: WifiMeasureReportData? = response.body()
            // on below line we are getting our data from model class
            // and adding it to our string.
            //val resp =
            //    "Response Code : " + response.code() + "\n" + "User Name : " + model!!.name + "\n" + "Job : " + model!!.job
            // below line we are setting our string to our response.
            //result.value = resp
        }

        override fun onFailure(call: Call<WifiMeasureReportData?>, t: Throwable) {
            // we get error response from API.
            Toast.makeText(ctx, "Error found is : " + t.message, Toast.LENGTH_SHORT).show()
        }
    })
}