package com.iotracks.iofabric.process_manager;

class ExecutionResponse {
   private int _statusCode;
   private String _stdout;
   private String _stderr;

   public ExecutionResponse(int statusCode, String stdout, String stderr) {
      _statusCode = statusCode;
      _stdout = stdout;
      _stderr = stderr;
   }

   public int getStatusCode() {
      return _statusCode;
   }

   public String getStdout() {
      return _stdout;
   }

   public String getStderr() {
      return _stderr;
   }
}
