package com.simats.digitallibrary;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Custom Volley Request for Multipart File Upload
 * Used for uploading book cover images
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String lineEnd = "\r\n";
    private final String twoHyphens = "--";

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;
    private Map<String, String> mParams;
    private Map<String, DataPart> mByteData;

    /**
     * DataPart class to hold file data
     */
    public static class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        public DataPart(String fileName, byte[] data, String mimeType) {
            this.fileName = fileName;
            this.content = data;
            this.type = mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * Constructor
     */
    public VolleyMultipartRequest(int method, String url,
            Response.Listener<NetworkResponse> listener,
            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // Add regular parameters
            if (mParams != null && mParams.size() > 0) {
                textParse(dos, mParams, getParamsEncoding());
            }

            // Add file data
            if (mByteData != null && mByteData.size() > 0) {
                dataParse(dos, mByteData);
            }

            // End multipart form data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new VolleyError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
    }

    /**
     * Parse regular text parameters
     */
    private void textParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding)
            throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildTextPart(dataOutputStream, entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encoding, uee);
        }
    }

    /**
     * Parse file data
     */
    private void dataParse(DataOutputStream dataOutputStream, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            buildDataPart(dataOutputStream, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Build individual text parameter
     */
    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue)
            throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    /**
     * Build file data part
     */
    private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataFile, String inputName)
            throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd);
        if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
            dataOutputStream.writeBytes("Content-Type: " + dataFile.getType() + lineEnd);
        }
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.write(dataFile.getContent());
        dataOutputStream.writeBytes(lineEnd);
    }

    /**
     * Set headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.mHeaders = headers;
    }

    /**
     * Set regular parameters
     */
    public void setParams(Map<String, String> params) {
        this.mParams = params;
    }

    /**
     * Set file data
     */
    public void setByteData(Map<String, DataPart> byteData) {
        this.mByteData = byteData;
    }
}
