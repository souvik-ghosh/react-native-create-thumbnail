require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-create-thumbnail"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-create-thumbnail
                   DESC
  s.homepage     = "https://github.com/souvik-ghosh/react-native-create-thumbnail"
  s.license      = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Souvik" => "emailtosvk@gmail.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/souvik-ghosh/react-native-create-thumbnail.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React-Core"
  # ...
  # s.dependency "..."
end

