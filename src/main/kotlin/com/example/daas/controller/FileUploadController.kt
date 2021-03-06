package com.example.daas.controller

import com.example.daas.service.storage.StorageService
import exception.StorageFileNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.IOException
import kotlin.streams.toList


@Controller
class FileUploadController @Autowired constructor(val storageService: StorageService) {
    @GetMapping("/file")
    @Throws(IOException::class)
    @RequestMapping
    fun listUploadedFiles(model: Model): String {
        model.addAttribute("files", storageService.loadAll().map { path ->
            MvcUriComponentsBuilder.fromMethodName(FileUploadController::class.java, "serveFile",
                    path.fileName.toString()).build().toString()
        }.toList())

        return "uploadForm"
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String): ResponseEntity<Resource> {

        val file = storageService.loadAsResource(filename)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body<Resource>(file)
    }

    @PostMapping("/file")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile,
                         redirectAttributes: RedirectAttributes): String {

        storageService.store(file)
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.originalFilename + "!")

        return "redirect:/file"
    }

    @ExceptionHandler(StorageFileNotFoundException::class)
    fun handleStorageFileNotFound(exc: StorageFileNotFoundException): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.OK).build<Any>()
    }
}