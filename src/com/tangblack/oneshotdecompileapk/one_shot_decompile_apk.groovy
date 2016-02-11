package com.tangblack.oneshotdecompileapk

import static groovy.io.FileType.FILES

/**
 * Created by tangblack on 2016/2/11.
 */

/**
 *
 * @param command
 * http://www.joergm.com/2010/09/executing-shell-commands-in-groovy/
 */
def void execute(String command)
{
//    println command.execute().text
    def process =
            new ProcessBuilder("bash", "-c", command).redirectErrorStream(true).start() // -c : Read commands from the following string and assign any arguments to the positional parameters.
    process.inputStream.eachLine {println it}
    process.waitFor()
}

def List listFilesMatching(String targetPath, String fileType)
{
    List fileList = new ArrayList()
    new File(targetPath).eachFileRecurse(FILES)
    {
        if(it.name.endsWith(fileType))
        {
            fileList.add(it.absolutePath)
        }
    }

    return fileList
}


println "Start..."


apkPath = "/path/to/your/apk"
outputPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "output"
apktoolOutputPath = "$outputPath/apktool"
unzipOutputPath= "$outputPath/unzip"
dex2jarOutputPath= "$outputPath/dex2jar"
unjarOutputPath= "$outputPath/unjar"
onejarOutputPath= "$outputPath/onejar"
jadOutputPath= "$outputPath/jad"


println "Make output folder."
execute "rm -rf $outputPath"
execute "mkdir $outputPath"


println "Unzip .apk"
execute "java -jar apktool/apktool_2.0.3.jar d -d $apkPath -o $apktoolOutputPath"


println "Rename .apk to .zip"
File apkFile = new File(apkPath)
zipName = apkFile.getName().replace(".apk", ".zip")
zipPath = "$outputPath/$zipName"
execute "cp $apkPath $zipPath"


println "Unzip .zip"
execute "unzip $zipPath -d $unzipOutputPath"


println "Translate all dex files to jar files."
dexFileList = listFilesMatching(unzipOutputPath, ".dex")
dexFileList.each
{
    jarName = new File(it).getName().replace(".dex", ".jar")
    execute "sh dex2jar/d2j-dex2jar.sh $it -o $dex2jarOutputPath/$jarName"
}


println "Unjar all jar files to class files."
jarFileList = listFilesMatching(dex2jarOutputPath, ".jar")
jarFileList.each
{
    execute "unzip $it -d $unjarOutputPath"
}



println "Combine all class files to one jar file."
execute "mkdir $onejarOutputPath"
execute "jar cf $onejarOutputPath/onejar.jar $unjarOutputPath"


println "Translate all class files to java files."
execute "jad/jad -r -ff -d $jadOutputPath -s java $unjarOutputPath/**/*.class"


println "Finish..."

