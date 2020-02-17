#include <jni.h>
#include <opencv2/opencv.hpp>


using namespace cv;

extern "C"
JNIEXPORT void JNICALL
// java 애서 imageprocessing 함수를 호출하면 cpp 함수에서 Mat 객체 주소와 함께 스레쏠드를 전달 받음
Java_com_example_opencvimageexample_MainActivity_imageprocessing(JNIEnv *env, jobject thiz,
                                                                 jlong input_image,
                                                                 jlong output_image,
                                                                 jint th1, jint th2 //자바에서 imageprocessing 호출 시 Canny의 스레솔드를 넘겨줌, cpp 파일에서 Canny 함수의 아규먼트로 사용함
                                                                 ) {
    // TODO: implement imageprocessing()
//    Mat 주소(Mat)를 cpp의 Mat 객체(img_input 또는 img_output)에 연결함
    Mat &img_input = *(Mat *) input_image;
    Mat &img_output = *(Mat *) output_image; // 에지 검출 결과는 output_image가 가리키는 자바 Mat 객체(img_output)에 저장됨

//    컬러 이미지를 그레이스케일 이미지로 변환함
    cvtColor( img_input, img_output, COLOR_RGB2GRAY);

//    블러로 잡음 제거 후 캐니로 에지를 검출
    blur( img_output, img_output, Size(5,5) );
    Canny( img_output, img_output, th1, th2); // 에지 검출 결과는 img_output에 저장, 자바의 Mat 객체에 연결되어 있기 때문에 자바에서도 결과 이미지에 접근 가능
}